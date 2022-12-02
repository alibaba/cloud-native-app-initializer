import PropTypes from 'prop-types'
import React from 'react'

const Header = ({ children }) => (
  <div className='header'>
    <h1 className='logo'>
      <a href='/bootstrap.html'>
        <span className='title'>
          <strong>云原生</strong>应用脚手架
        </span>
        <span className='description'>Cloud Native App Initializer</span>
      </a>
    </h1>
    {children}
  </div>
)

Header.defaultProps = {
  children: null,
}

Header.propTypes = {
  children: PropTypes.node,
}

export default Header
