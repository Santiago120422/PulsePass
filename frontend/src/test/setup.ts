// jsdom exposes dialog elements but does not implement the modal methods.
const dialog = HTMLDialogElement.prototype
if (!dialog.showModal) {
  dialog.showModal = function () { this.open = true }
  dialog.close = function () { this.open = false }
}
