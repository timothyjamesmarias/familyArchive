// Common utility functions shared across the application

export const dom = {
  /**
   * Safely select an element with type checking
   */
  select: <T extends HTMLElement>(selector: string): T | null => {
    return document.querySelector<T>(selector)
  },

  /**
   * Select all elements matching a selector
   */
  selectAll: <T extends HTMLElement>(selector: string): T[] => {
    return Array.from(document.querySelectorAll<T>(selector))
  },
}

export const http = {
  /**
   * Simple fetch wrapper with JSON handling
   */
  get: async <T>(url: string): Promise<T> => {
    const response = await fetch(url)
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`)
    }
    return response.json()
  },

  /**
   * POST request with JSON body
   */
  post: async <T>(url: string, data: unknown): Promise<T> => {
    const response = await fetch(url, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(data),
    })
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`)
    }
    return response.json()
  },
}
