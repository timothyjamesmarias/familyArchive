# Development Guide

## Frontend Setup

This project uses **Vite** for frontend tooling with **Tailwind CSS** for styling.

### Directory Structure

```
src/main/frontend/
├── css/
│   └── main.css              # Tailwind entry point
├── js/
│   ├── main.ts              # Main application entry
│   ├── common/              # Shared utilities
│   │   └── utils.ts
│   └── apps/                # Embedded SPAs
│       └── example-spa.ts
```

## Development Workflow

### Option 1: Production Build (via Gradle)

Build the entire application including frontend:

```bash
./gradlew build
```

This will:
1. Install npm dependencies
2. Build frontend with Vite
3. Output to `src/main/resources/static/dist/`
4. Build the Spring Boot application

Run the application:

```bash
./gradlew bootRun
```

### Option 2: Development Mode with Live Reload (Recommended)

For the best development experience with instant feedback:

**Terminal 1 - Start Vite dev server:**
```bash
npm install  # First time only
npm run dev
```

**Terminal 2 - Start Spring Boot:**
```bash
./gradlew bootRun --args='--spring.profiles.active=dev'
```

Then open `http://localhost:8080` in your browser.

**How it works:**
- Spring Boot serves the application on port 8080
- Vite dev server runs on port 5173
- In dev mode, templates load assets from Vite dev server
- Changes to CSS/JS are instantly reflected (HMR)
- Spring DevTools reloads on Kotlin/template changes

## Adding Frontend Code

### Adding Global Styles

Edit `src/main/frontend/css/main.css`:

```css
@layer components {
  .my-custom-class {
    @apply px-4 py-2 bg-blue-500;
  }
}
```

### Adding Global JavaScript

Edit `src/main/frontend/js/main.ts` or create new modules in `js/common/`.

### Creating an Embedded SPA

1. Create a new file in `src/main/frontend/js/apps/my-spa.ts`
2. Add it to Vite config `rollupOptions.input`
3. Load it in your Thymeleaf template:

```html
<th:block layout:fragment="scripts">
    <th:block th:if="${@environment.getActiveProfiles().contains('dev')}">
        <script type="module" src="http://localhost:5173/js/apps/my-spa.ts"></script>
    </th:block>
    <th:block th:unless="${@environment.getActiveProfiles().contains('dev')}">
        <script type="module" th:src="@{/dist/assets/my-spa.js}"></script>
    </th:block>
</th:block>
```

## Templates

Templates use **Thymeleaf Layout Dialect**. All pages should extend `layout.html`:

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org"
      xmlns:layout="http://www.ultraq.net.nz/thymeleaf/layout"
      layout:decorate="~{layout}">

<div layout:fragment="content">
    <!-- Your page content -->
</div>

</html>
```

## Tailwind CSS

Use Tailwind utility classes in your templates:

```html
<button class="btn-primary">Click me</button>
<div class="bg-blue-500 text-white p-4 rounded">Card</div>
```

Custom components are defined in `src/main/frontend/css/main.css`.

## TypeScript

The project uses TypeScript for better type safety. You can use modern JS features - Vite will transpile everything.

## Troubleshooting

**Assets not loading in dev mode:**
- Make sure Vite dev server is running (`npm run dev`)
- Check that port 5173 is not blocked
- Verify `spring.profiles.active=dev` is set

**Build fails:**
- Delete `node_modules` and run `npm install` again
- Clear Gradle cache: `./gradlew clean`
- Check that Node.js is downloaded: `.gradle/nodejs/`

**Hot reload not working:**
- Vite HMR only works in dev mode with Vite dev server
- Spring DevTools handles backend/template reloads
- Browser extensions may interfere with WebSocket connections

## Admin Panel Architecture

This project uses a CMS/admin panel for content management. Here's how it's organized:

### Common Spring Patterns for Admin Panels

Unlike Rails' base controller approach, Spring uses more declarative patterns:

**1. Package-based organization**
```
controller/
├── HomeController.kt          # Public controllers
├── LoginController.kt
└── admin/                     # Admin controllers in separate package
    ├── AdminControllerAdvice.kt
    ├── AdminDashboardController.kt
    └── AdminUsersController.kt
```

**2. Path-based security**
All admin routes use the `/admin/**` prefix and are protected in `SecurityConfig`:

```kotlin
.requestMatchers("/admin/**").authenticated()
```

**3. @ControllerAdvice for common attributes**
The `AdminControllerAdvice` (src/main/kotlin/com/timothymarias/familyarchive/controller/admin/AdminControllerAdvice.kt:1) automatically adds common model attributes to all admin controllers. This is equivalent to Rails' `before_action` in a base controller:

```kotlin
@ControllerAdvice(basePackages = ["com.timothymarias.familyarchive.controller.admin"])
class AdminControllerAdvice {
    @ModelAttribute
    fun addUserToModel(model: Model, @AuthenticationPrincipal currentUser: UserDetails?) {
        currentUser?.let { model.addAttribute("currentUser", it) }
    }
}
```

**4. Separate layouts**
Admin pages use `admin/layout.html` which includes:
- Admin navigation sidebar
- User info in header
- Different styling from public pages

### Creating a New Admin Controller

Example:

```kotlin
@Controller
@RequestMapping("/admin/users")
class AdminUsersController(
    private val userRepository: UserRepository
) {
    @GetMapping
    fun index(model: Model): String {
        model.addAttribute("pageTitle", "Manage Users")
        model.addAttribute("users", userRepository.findAll())
        // currentUser is automatically available from AdminControllerAdvice
        return "admin/users/index"
    }
}
```

Create the corresponding template in `src/main/resources/templates/admin/users/index.html`:

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org"
      xmlns:layout="http://www.ultraq.net.nz/thymeleaf/layout"
      layout:decorate="~{admin/layout}">

<div layout:fragment="content">
    <!-- Your admin page content -->
</div>

</html>
```

### Authentication & Authorization

**Login:**
- URL: `http://localhost:8080/login`
- Default credentials: `admin@example.com` / `password`

**Security:**
- All `/admin/**` routes require authentication
- Passwords are hashed with BCrypt
- Form-based login with CSRF protection
- Users are stored in the `users` table

**Adding new admins:**
Use Spring's `PasswordEncoder` in a controller or service:

```kotlin
@Autowired
private lateinit var passwordEncoder: PasswordEncoder

val user = User(
    email = "newadmin@example.com",
    password = passwordEncoder.encode("password") ?: throw IllegalStateException("Password encoding failed"),
    name = "New Admin"
)
userRepository.save(user)
```

### Database Seeding

The project uses a dedicated `DatabaseSeeder` (src/main/kotlin/com/timothymarias/familyarchive/seeder/DatabaseSeeder.kt:1) instead of migrations for seeding data:

**How it works:**
- Implements `ApplicationRunner` to run on startup
- Only runs when `app.seeding.enabled=true`
- Enabled automatically in dev profile (application-dev.properties)
- Checks if data exists before seeding to prevent duplicates

**Adding more seed data:**
Just add methods to `DatabaseSeeder`:

```kotlin
private fun seedCategories() {
    if (categoryRepository.count() > 0) {
        logger.info("Categories already exist, skipping")
        return
    }

    logger.info("Seeding categories...")
    // Your seeding logic
}
```

Then call it from the `run()` method:

```kotlin
override fun run(args: ApplicationArguments) {
    logger.info("Starting database seeding...")
    seedUsers()
    seedCategories()  // Add your new seeder here
    logger.info("Database seeding completed!")
}
```

**Controlling seeding:**
- Dev mode: Seeding enabled by default
- Production: Set `app.seeding.enabled=true` to enable (not recommended)
- The seeder is idempotent - it won't duplicate data if run multiple times

### Flash Messages

The admin layout supports flash messages via RedirectAttributes:

```kotlin
@PostMapping("/admin/users")
fun create(@ModelAttribute user: User, redirectAttributes: RedirectAttributes): String {
    userRepository.save(user)
    redirectAttributes.addFlashAttribute("successMessage", "User created successfully!")
    return "redirect:/admin/users"
}
```

### Adding Common Navigation Items

Update `AdminControllerAdvice` to add navigation items available to all admin pages:

```kotlin
@ModelAttribute
fun addNavigation(model: Model) {
    model.addAttribute("navItems", listOf(
        NavItem("Dashboard", "/admin/dashboard"),
        NavItem("Users", "/admin/users"),
        NavItem("Content", "/admin/content")
    ))
}
```
