import '../../../styles/sandbox.scss'
import PropTypes from 'prop-types'
import React from 'react'

function SandboxShell({position}) {
  return (
    <button
      className='button primary share-ghost'
      type='button'
      style={{
        top: `${position.y}px`,
        left: `${position.x}px`,
      }}
    >
      跑一下 ^_^
    </button>
  )
}

SandboxShell.propTypes = {
  position: PropTypes.shape({
    x: PropTypes.number.isRequired,
    y: PropTypes.number.isRequired,
  }).isRequired,
}

export default SandboxShell
