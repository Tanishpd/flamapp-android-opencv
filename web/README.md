# Web Viewer

TypeScript-based web viewer for displaying processed frames from FlamApp.

## Setup

```bash
npm install
npm run build
```

## Run

```bash
# Option 1: Use http-server
npx http-server .

# Option 2: Use Python
python3 -m http.server 8000

# Option 3: Use Node.js
npx serve .
```

Then open http://localhost:8000 in your browser.

## Features

- Display processed camera frames
- Show FPS and resolution info
- Toggle between processing modes
- Responsive design

## API

The viewer exposes a global `flamAppViewer` object:

```typescript
// Display a frame
flamAppViewer.displayFrame({
    width: 640,
    height: 480,
    data: new Uint8ClampedArray(...),
    mode: 'Canny'
});

// Update FPS
flamAppViewer.updateFPS(15.0);
```
