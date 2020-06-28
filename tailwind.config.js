module.exports = {
  purge: ['./src/**/*.clj'],
  theme: {
    extend: {
      colors: {
        indigo: '#5408d6',
        cyan: '#63ffdb',
      },
    },
  },
  variants: {
    borderWidth: ['responsive', 'first', 'hover', 'focus'],
  },
  plugins: [],
}
