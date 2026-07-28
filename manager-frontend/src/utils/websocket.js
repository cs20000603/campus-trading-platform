import { Client } from '@stomp/stompjs'

const WS_URL = import.meta.env.VITE_BASE_URL.replace('http', 'ws') + '/ws'

let stompClient = null
let connected = false
let listeners = []

export function connect() {
  if (stompClient && connected) return

  stompClient = new Client({
    brokerURL: WS_URL,
    reconnectDelay: 5000,
    heartbeatIncoming: 4000,
    heartbeatOutgoing: 4000,
    onConnect: () => {
      connected = true
      console.log('[WS] 管理员后台 WebSocket 已连接')
      stompClient.subscribe('/topic/admin', msg => {
        try {
          const data = JSON.parse(msg.body)
          notifyListeners(data)
        } catch (e) { /* ignore */ }
      })
    },
    onDisconnect: () => {
      connected = false
      console.log('[WS] WebSocket 已断开')
    },
    onStompError: frame => {
      console.error('[WS] STOMP 错误:', frame.headers['message'])
    }
  })

  stompClient.activate()
}

export function disconnect() {
  if (stompClient) {
    stompClient.deactivate()
    stompClient = null
  }
  connected = false
}

export function onMessage(callback) {
  listeners.push(callback)
}

export function offMessage(callback) {
  listeners = listeners.filter(fn => fn !== callback)
}

function notifyListeners(data) {
  listeners.forEach(fn => {
    try { fn(data) } catch (e) { /* ignore */ }
  })
}

export default { connect, disconnect, onMessage, offMessage }
