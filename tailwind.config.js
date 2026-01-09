/** @type {import('tailwindcss').Config} */
export default {
  darkMode: 'class', // Enable class-based dark mode
  content: [
    './src/main/frontend/**/*.{js,ts,jsx,tsx}',
    './src/main/resources/templates/**/*.html',
  ],
  theme: {
    extend: {},
  },
  plugins: [],
}
