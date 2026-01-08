// Example embedded SPA
// This would be loaded on specific pages that need this functionality

export class ExampleSPA {
  private container: HTMLElement

  constructor(containerId: string) {
    const element = document.getElementById(containerId)
    if (!element) {
      throw new Error(`Container ${containerId} not found`)
    }
    this.container = element
    this.init()
  }

  private init() {
    this.render()
    this.attachEventListeners()
  }

  private render() {
    this.container.innerHTML = `
      <div class="p-4 bg-white rounded-lg shadow">
        <h2 class="text-2xl font-bold mb-4">Example SPA</h2>
        <p class="text-gray-600 mb-4">This is an embedded single-page application.</p>
        <button class="btn-primary" data-action="greet">Click me</button>
      </div>
    `
  }

  private attachEventListeners() {
    this.container.querySelector('[data-action="greet"]')?.addEventListener('click', () => {
      alert('Hello from the embedded SPA!')
    })
  }
}

// Auto-initialize if container exists
if (document.getElementById('example-spa-container')) {
  new ExampleSPA('example-spa-container')
}
