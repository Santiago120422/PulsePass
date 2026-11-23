import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { ticketsApi } from './api'
import { type Ticket } from './tipos'

export function useTicket(code: string) {
  return useQuery({ queryKey: ['ticket', code], queryFn: ({ signal }) => ticketsApi.ticket(code, signal), enabled: !!code })
}

export function useTicketPrices(enabled: boolean) {
  return useQuery({ queryKey: ['ticket-prices'], queryFn: ({ signal }) => ticketsApi.prices(signal), enabled })
}

export function useTicketAction(action: (code: string) => Promise<Ticket>, onSuccess?: () => void) {
  const client = useQueryClient()
  return useMutation({ mutationFn: action, onSuccess: ticket => {
    client.setQueryData(['ticket', ticket.ticketCode], ticket)
    client.setQueriesData<Ticket[]>({ queryKey: ['tickets'] }, items =>
      items?.map(item => item.ticketCode === ticket.ticketCode ? ticket : item))
    void client.invalidateQueries({ queryKey: ['tickets'] })
    void client.invalidateQueries({ queryKey: ['paid', ticket.eventCode] })
    void client.invalidateQueries({ queryKey: ['events'] })
    void client.invalidateQueries({ queryKey: ['event', ticket.eventCode] })
    onSuccess?.()
  } })
}

export function useTickets(email: string) {
  return useQuery({ queryKey: ['tickets', email], queryFn: ({ signal }) => ticketsApi.tickets(email, signal), enabled: !!email })
}

export function usePaidTickets(code: string) {
  return useQuery({ queryKey: ['paid', code], queryFn: ({ signal }) => ticketsApi.paid(code, signal), enabled: !!code })
}

export function usePurchase(code: string, type: Ticket['type'], onSuccess: (ticket: Ticket) => void) {
  const client = useQueryClient()
  return useMutation({
    mutationFn: (userEmail: string) => ticketsApi.purchase({ type, userEmail, eventCode: code }),
    onSuccess: ticket => {
      client.setQueryData(['ticket', ticket.ticketCode], ticket)
      void client.invalidateQueries({ queryKey: ['tickets'] })
      void client.invalidateQueries({ queryKey: ['paid', code] })
      void client.invalidateQueries({ queryKey: ['event', code] })
      void client.invalidateQueries({ queryKey: ['events'] })
      onSuccess(ticket)
    },
  })
}
