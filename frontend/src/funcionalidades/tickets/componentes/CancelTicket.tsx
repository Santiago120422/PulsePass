import { useState } from 'react'
import { ErrorNote } from '../../../componentes/Aviso'
import { Modal } from '../../../componentes/Modal'
import { ticketsApi } from '../api'

import { useTicketAction } from '../hooks'

export function CancelTicket({ code, onCancel }: { code: string; onCancel: () => void }) {
  const [confirm, setConfirm] = useState(false)
  const cancel = useTicketAction(ticketsApi.cancel, () => { setConfirm(false); onCancel() })
  return <>
    <button className="danger" type="button" onClick={() => { cancel.reset(); setConfirm(true) }}>Cancelar ticket</button>
    {confirm && <Modal title="¿Cancelar este ticket?" close={() => setConfirm(false)} busy={cancel.isPending}>
      <p>Confirma la cancelación de la entrada {code}.</p>
      {cancel.error && <ErrorNote error={cancel.error} />}
      <button className="danger" disabled={cancel.isPending} onClick={() => cancel.mutate(code)}>{cancel.isPending ? 'Cancelando…' : 'Confirmar cancelación'}</button>
    </Modal>}
  </>
}
