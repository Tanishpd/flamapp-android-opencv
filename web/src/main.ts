/**
 * FlamApp Web Viewer
 * Displays processed frames from Android OpenCV processing
 */

interface FrameData {
    width: number;
    height: number;
    data: Uint8ClampedArray;
    mode: string;
}

class FlamAppViewer {
    private canvas: HTMLCanvasElement;
    private ctx: CanvasRenderingContext2D;
    private fpsElement: HTMLElement;
    private resolutionElement: HTMLElement;
    private modeElement: HTMLElement;
    private statusElement: HTMLElement;

    private currentMode: string = 'Canny';
    private fps: number = 12.5;
    private resolution: { width: number; height: number } = { width: 640, height: 480 };

    constructor() {
        this.canvas = document.getElementById('displayCanvas') as HTMLCanvasElement;
        this.ctx = this.canvas.getContext('2d')!;
        this.fpsElement = document.getElementById('fpsValue')!;
        this.resolutionElement = document.getElementById('resolutionValue')!;
        this.modeElement = document.getElementById('modeValue')!;
        this.statusElement = document.getElementById('statusText')!;

        this.initializeCanvas();
        this.setupEventListeners();
        this.updateDisplay();
        this.loadSampleFrame();
    }

    private initializeCanvas(): void {
        this.canvas.width = this.resolution.width;
        this.canvas.height = this.resolution.height;
        
        // Draw initial pattern
        this.ctx.fillStyle = '#000000';
        this.ctx.fillRect(0, 0, this.canvas.width, this.canvas.height);
        
        this.ctx.fillStyle = '#00FF00';
        this.ctx.font = '24px Arial';
        this.ctx.textAlign = 'center';
        this.ctx.fillText('Waiting for frame...', this.canvas.width / 2, this.canvas.height / 2);
    }

    private setupEventListeners(): void {
        const loadBtn = document.getElementById('loadBtn');
        const toggleBtn = document.getElementById('toggleBtn');
        const refreshBtn = document.getElementById('refreshBtn');

        loadBtn?.addEventListener('click', () => this.loadSampleFrame());
        toggleBtn?.addEventListener('click', () => this.toggleMode());
        refreshBtn?.addEventListener('click', () => this.refreshStats());
    }

    private loadSampleFrame(): void {
        this.updateStatus('Loading sample frame...');
        
        // Generate a sample processed frame (Canny edge detection simulation)
        const imageData = this.ctx.createImageData(this.canvas.width, this.canvas.height);
        
        if (this.currentMode === 'Canny') {
            this.generateCannyPattern(imageData);
        } else {
            this.generateGrayscalePattern(imageData);
        }
        
        this.ctx.putImageData(imageData, 0, 0);
        this.updateStatus('Sample frame loaded successfully');
        
        // Simulate FPS variation
        this.fps = 10 + Math.random() * 5;
        this.updateDisplay();
    }

    private generateCannyPattern(imageData: ImageData): void {
        const data = imageData.data;
        const width = this.canvas.width;
        const height = this.canvas.height;

        // Create edge pattern
        for (let y = 0; y < height; y++) {
            for (let x = 0; x < width; x++) {
                const i = (y * width + x) * 4;
                
                // Create edge-like patterns
                const edge = (
                    Math.sin(x / 20) * Math.cos(y / 20) +
                    Math.sin(x / 10) * Math.cos(y / 30) +
                    Math.cos((x + y) / 15)
                ) > 0.5;
                
                const value = edge ? 255 : 0;
                
                data[i] = value;     // R
                data[i + 1] = value; // G
                data[i + 2] = value; // B
                data[i + 3] = 255;   // A
            }
        }
    }

    private generateGrayscalePattern(imageData: ImageData): void {
        const data = imageData.data;
        const width = this.canvas.width;
        const height = this.canvas.height;

        // Create grayscale gradient pattern
        for (let y = 0; y < height; y++) {
            for (let x = 0; x < width; x++) {
                const i = (y * width + x) * 4;
                
                const value = Math.floor(
                    128 + 127 * Math.sin(x / 50) * Math.cos(y / 50)
                );
                
                data[i] = value;     // R
                data[i + 1] = value; // G
                data[i + 2] = value; // B
                data[i + 3] = 255;   // A
            }
        }
    }

    private toggleMode(): void {
        this.currentMode = this.currentMode === 'Canny' ? 'Gray' : 'Canny';
        this.updateDisplay();
        this.loadSampleFrame();
        this.updateStatus(`Switched to ${this.currentMode} mode`);
    }

    private refreshStats(): void {
        // Simulate FPS variation
        this.fps = 10 + Math.random() * 5;
        this.updateDisplay();
        this.updateStatus('Statistics refreshed');
    }

    private updateDisplay(): void {
        this.fpsElement.textContent = this.fps.toFixed(1);
        this.resolutionElement.textContent = `${this.resolution.width}x${this.resolution.height}`;
        this.modeElement.textContent = this.currentMode;
    }

    private updateStatus(message: string): void {
        this.statusElement.textContent = message;
        console.log(`[FlamApp] ${message}`);
    }

    public displayFrame(frameData: FrameData): void {
        this.canvas.width = frameData.width;
        this.canvas.height = frameData.height;
        this.resolution = { width: frameData.width, height: frameData.height };

        const imageData = this.ctx.createImageData(frameData.width, frameData.height);
        imageData.data.set(frameData.data);
        this.ctx.putImageData(imageData, 0, 0);

        this.currentMode = frameData.mode;
        this.updateDisplay();
        this.updateStatus('Frame displayed successfully');
    }

    public updateFPS(fps: number): void {
        this.fps = fps;
        this.updateDisplay();
    }
}

// Initialize viewer when DOM is ready
document.addEventListener('DOMContentLoaded', () => {
    console.log('[FlamApp] Initializing web viewer...');
    const viewer = new FlamAppViewer();
    
    // Expose to window for external access
    (window as any).flamAppViewer = viewer;
    
    console.log('[FlamApp] Web viewer initialized successfully');
});

export { FlamAppViewer, FrameData };
