import PropTypes from 'prop-types'
import React from 'react'

const Switch = ({ id, isOn, onChange }) => {
  return (
    <span className='switch'>
      <input
        checked={isOn}
        onChange={onChange}
        className='switch-checkbox'
        id={id}
        name='switch-new'
        type='checkbox'
      />
      {/* eslint-disable-next-line */}
      <label className='switch-label' htmlFor={id}>
        <span className='switch-button' />
      </label>
    </span>
  )
}

Switch.defaultProps = {
  isOn: false,
  onChange: null,
}

Switch.propTypes = {
  isOn: PropTypes.bool,
  onChange: PropTypes.func,
}

export default Switch
