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
