import {useState} from 'react'

function getProperties() {
  return {
    symb:
      window.navigator.userAgent.toLowerCase().indexOf('mac') > -1
        ? '⌘'
        : 'Ctrl',
    origin: window.location.origin,
    pathname: window.location.pathname,
  }
}

export default function useWindowsUtils() {
  const [symb] = useState(getProperties().symb)
  const [origin] = useState(getProperties().origin)
  const [pathname] = useState(getProperties().pathname)
  return { symb, origin, pathname }
}
