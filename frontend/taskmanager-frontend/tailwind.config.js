/** @type {import('tailwindcss').Config} */
module.exports = {
  // "content" sagt Tailwind: Schau in DIESEN Dateien nach Klassen
  // Nur Klassen die du wirklich nutzt, werden ins finale CSS gepackt
  content: [
    "./src/**/*.{html,ts}"  // ← Alle HTML und TypeScript Dateien im src Ordner
  ],
  theme: {
    extend: { // Hier kannst du eigene Farben/Größen hinzufügen
      colors: {
        primary: {
          50:  '#eff6ff',
          500: '#3b82f6',
          600: '#2563eb',
          700: '#1d4ed8',
        }
      }
    },
  },
  plugins: [],
}

