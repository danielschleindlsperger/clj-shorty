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

function copyLinkToClipboard() {
  for (const button of document.querySelectorAll('button[data-clipboard]')) {
    button.addEventListener('click', function () {
      const url = button.dataset.clipboard
      navigator.clipboard.writeText(url).then(console.log)
    })
  }
}

ready(function () {
  autoFocusInput()
  copyLinkToClipboard()
})
