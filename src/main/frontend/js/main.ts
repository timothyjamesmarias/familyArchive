// Main application entry point
import './common/darkMode'

console.log('Family Archive - Frontend initialized')

// Example: Initialize common functionality
document.addEventListener('DOMContentLoaded', () => {
  console.log('DOM fully loaded')

  // Your common initialization code here
})

// Export utilities for use in other modules
export const utils = {
  formatDate: (date: Date) => {
    return new Intl.DateTimeFormat('en-US', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
    }).format(date)
  },
}
