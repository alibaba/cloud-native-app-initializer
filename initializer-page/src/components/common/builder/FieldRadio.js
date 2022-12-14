import PropTypes from 'prop-types'
import React from 'react'

import {RadioGroup} from '../form'

const FieldRadio = ({ id, text, value, onChange, disabled, options }) => (
  <div className='control'>
    <label htmlFor={id}>{text}</label>
    <RadioGroup
      name='packaging'
      disabled={disabled}
      selected={value}
      options={options}
      onChange={onChange}
    />
  </div>
)

FieldRadio.defaultProps = {
  disabled: false,
  options: [],
}

FieldRadio.propTypes = {
  id: PropTypes.string.isRequired,
  text: PropTypes.string.isRequired,
  value: PropTypes.string.isRequired,
  onChange: PropTypes.func.isRequired,
  disabled: PropTypes.bool,
  options: PropTypes.arrayOf(
    PropTypes.shape({
      key: PropTypes.string,
      text: PropTypes.string,
    })
  ),
}

export default FieldRadio
