/** @type {import('tailwindcss').Config} */
module.exports = {
  content: ["./src/**/*.{html,ts,scss}"],
  theme: {
    extend: {
      colors: {
        primary: "#0d2a66",              // Hyundai blue
        "primary-light": "#E3F2FD",      // Light blue
        "text-gray": "#6C757D",          // Medium gray
        "border-gray": "#ADB5BD",        // Border colors
        "success-green": "#198754",      // Success states (darker green)
        "error-red": "#DC3545",          // Error states (Bootstrap red)
        "warning-yellow": "#FFC107",     // Warning states (Bootstrap warning)
        "gray-50": "#F8F9FA",           // Light backgrounds
        "gray-100": "#E9ECEF",          // Card backgrounds
        "stone-100": "#F8F9FA",         // Dropdown backgrounds
      },
      fontFamily: {
        outfit: ["Outfit", "serif"],     // Exact from reference
      },
      gridTemplateColumns: {
        auto: "repeat(auto-fill, minmax(200px, 1fr))",  // Exact from reference
      },
      animation: {
        "fade-in": "fadeIn 0.3s ease-in-out",
        "slide-up": "slideUp 0.3s ease-out", 
        "scale-in": "scaleIn 0.2s ease-out",
      },
      keyframes: {
        fadeIn: {
          '0%': { opacity: '0' },
          '100%': { opacity: '1' },
        },
        slideUp: {
          '0%': { transform: 'translateY(20px)', opacity: '0' },
          '100%': { transform: 'translateY(0)', opacity: '1' },
        },
        scaleIn: {
          '0%': { transform: 'scale(0.95)', opacity: '0' },
          '100%': { transform: 'scale(1)', opacity: '1' },
        },
      },
    },
  },
  plugins: [],
}; 