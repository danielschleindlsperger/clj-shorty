function ready(fn) {
  if (document.readyState !== 'loading') {
    fn()
  } else {
    document.addEventListener('DOMContentLoaded', fn)
  }
}

function autoFocusInput() {
  var input = document.querySelector('input[name="url"]')

  if (input) {
    input.focus()
  }
}

ready(function () {
  autoFocusInput()
})
