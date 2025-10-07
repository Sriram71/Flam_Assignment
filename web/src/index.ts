import './styles.css';

/**
 * ImageViewer class for displaying processed images with stats overlay
 */
class ImageViewer {
    private imageElement: HTMLImageElement;
    private fpsCounter: HTMLElement;
    private resolutionDisplay: HTMLElement;
    private fileInput: HTMLInputElement;
    private demoButton: HTMLButtonElement;
    
    // Demo image (base64 encoded small gray gradient)
    private readonly demoImageBase64 = 'data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAQAAAAEACAIAAADTED8xAAADMElEQVR4nOzVMQEAIAzAMMC/5yFjRxMFfXpnZg5Eve0A2GQAhA2AsAEQNgDCBkDYAAh/6QswzgQIGwBhAyBsAIQNgLABEDYAwgZA2AAIGwBhAyBsAIQNgLABEDYAwgZA2AAIGwBhAyBsAIQNgLABEDYAwgZA2AAIGwBhAyBsAIQNgLABEDYAwgZA2AAIGwBhAyBsAIQNgLABEDYAwgZA2AAIGwBhAyBsAIQNgLABEDYAwgZA2AAIGwBhAyBsAIQNgLABEDYAwgZA2AAIGwBhAyBsAIQNgLABEDYAwgZA2AAIGwBhAyBsAIQNgLABEDYAwgZA2AAIGwBhAyBsAIQNgLABEDYAwgZA2AAIGwBhAyBsAIQNgLABEDYAwgZA2AAIGwBhAyBsAIQNgLABEDYAwgZA2AAIGwBhAyBsAIQNgLABEDYAwgZA2AAIGwBhAyBsAIQNgLABEDYAwgZA2AAIGwBhAyBsAIQNgLABEDYAwgZA2AAIGwBhAyBsAIQNgLABEDYAwgZA2AAIGwBhAyBsAIQNgLABEDYAwgZA2AAIGwBhAyBsAIQNgLABEDYAwgZA2AAIGwBhAyBsAIQNgLABEDYAwgZA2AAIGwBhAyBsAIQNgLABEDYAwgZA2AAIGwBhAyBsAIQNgLABEDYAwgZA2AAIGwBhAyBsAIQNgLABEDYAwgZA2AAIGwBhAyBsAIQNgLABEDYAwgZA2AAIGwBhAyBsAIQNgLABEDYAwgZA2AAIGwBhAyBsAIQNgLABEDYAwgZA2AAIGwBhAyBsAIQNgLABEDYAwgZA2AAIGwBhAyBsAIQNgLABEDYAwgZA2AAIGwBhAyBsAIQNgLABEDYAwgZA2AAIGwBhAyBsAIQNgLABEDYAwgZA2AAIGwBhAyBsAIQNgLABEDYAwgZA2AAIGwBhAyBsAIQNgLABEDYAwgZA2AAIGwBhAyBsAIQNgLABEDYAwgZA2AAIGwBhAyBsAIQNgLABEDYAwgZA2AAIGwBhAyBsAIQNgLABEDYAwgZA2AAIGwBhAyBsAIQNgLABEDYAwgZA2AAIGwBhAyBsAIQNgLABEDYAwgZA2AAIfwGL2ADCnYcOAAAAAElFTkSuQmCC';
    
    // Stats
    private fps: number = 0;
    private imageWidth: number = 0;
    private imageHeight: number = 0;
    
    constructor() {
        // Get DOM elements
        this.imageElement = document.getElementById('processed-image') as HTMLImageElement;
        this.fpsCounter = document.getElementById('fps-counter') as HTMLElement;
        this.resolutionDisplay = document.getElementById('resolution-display') as HTMLElement;
        this.fileInput = document.getElementById('image-input') as HTMLInputElement;
        this.demoButton = document.getElementById('demo-button') as HTMLButtonElement;
        
        // Set up event listeners
        this.fileInput.addEventListener('change', this.handleFileSelect.bind(this));
        this.demoButton.addEventListener('click', this.loadDemoImage.bind(this));
        
        // Load demo image by default
        this.loadDemoImage();
    }
    
    /**
     * Handle file selection from input
     */
    private handleFileSelect(event: Event): void {
        const input = event.target as HTMLInputElement;
        
        if (input.files && input.files[0]) {
            const reader = new FileReader();
            
            reader.onload = (e: ProgressEvent<FileReader>) => {
                const result = e.target?.result as string;
                this.displayImage(result);
            };
            
            reader.readAsDataURL(input.files[0]);
        }
    }
    
    /**
     * Load demo image
     */
    private loadDemoImage(): void {
        this.displayImage(this.demoImageBase64);
        
        // Simulate FPS for demo
        this.fps = Math.floor(Math.random() * 20) + 10; // Random FPS between 10-30
        this.updateStats();
    }
    
    /**
     * Display image and update stats
     */
    private displayImage(src: string): void {
        // Create a new image to get dimensions
        const img = new Image();
        
        img.onload = () => {
            // Update image dimensions
            this.imageWidth = img.width;
            this.imageHeight = img.height;
            
            // Update stats display
            this.updateStats();
            
            // Display the image
            this.imageElement.src = src;
        };
        
        img.src = src;
    }
    
    /**
     * Update stats display
     */
    private updateStats(): void {
        this.fpsCounter.textContent = `FPS: ${this.fps}`;
        this.resolutionDisplay.textContent = `Resolution: ${this.imageWidth} x ${this.imageHeight}`;
    }
    
    /**
     * Update image from base64 data (to be called from Android app)
     */
    public updateImageFromBase64(base64Data: string, fps: number): void {
        this.fps = fps;
        this.displayImage(base64Data);
    }
}

// Initialize the viewer when the DOM is loaded
document.addEventListener('DOMContentLoaded', () => {
    // Create global instance for access from Android WebView
    (window as any).imageViewer = new ImageViewer();
});