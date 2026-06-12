const queue: string[] = []
let timer: any = null

export function ElMessage(msg: string) {
  queue.push(msg)
  if (timer) return
  const toast = document.createElement('div')
  toast.className = 'toast-msg'
  document.body.appendChild(toast)

  function show() {
    if (queue.length === 0) { toast.remove(); timer = null; return }
    toast.textContent = queue.shift()!
    toast.classList.add('show')
    clearTimeout(timer)
    timer = setTimeout(() => { toast.classList.remove('show'); timer = setTimeout(show, 200) }, 2200)
  }
  show()
}
