/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        primary: '#1A73E8',
        'primary-dark': '#0D47A1',
        accent: '#FF6F00',
        'accent-light': '#FFF3E0',
        success: '#2E7D32',
        warning: '#F9A825',
        danger: '#C62828',
        dark: '#0D2137',
        slate: '#455A64',
        border: '#E3EAF4',
        'bg-light': '#F7F9FC',
      },
      fontFamily: {
        sans: ['Plus Jakarta Sans', 'Nunito', 'ui-sans-serif', 'system-ui'],
      },
      borderRadius: {
        card: '16px',
      },
      boxShadow: {
        card: '0 2px 12px rgba(26, 115, 232, 0.06)',
        'card-hover': '0 8px 32px rgba(26, 115, 232, 0.15)',
      },
      animation: {
        'pulse-slow': 'pulse 3s cubic-bezier(0.4, 0, 0.6, 1) infinite',
        'float': 'float 3s ease-in-out infinite',
      },
      keyframes: {
        float: {
          '0%, 100%': { transform: 'translateY(0)' },
          '50%': { transform: 'translateY(-6px)' },
        },
      },
    },
  },
  plugins: [],
}
