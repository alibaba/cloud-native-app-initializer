import PropTypes from 'prop-types'
import React from 'react'

const Footer = ({ children }) => (
  <div className='sticky'>
    <div className='colset colset-submit'>
      <div className='left nopadding'>
        <footer className='footer'>
          <div className='footer-container'>
            © 1999-{new Date().getFullYear()} Aliyun.com
            <br />
            start.aliyun.com is powered by
            <br />
            <span>
              <a
                tabIndex='-1'
                target='_blank'
                rel='noopener noreferrer'
                href='https://www.aliyun.com'
              >
                Aliyun.com
              </a>
            </span>
          </div>
        </footer>
      </div>
      {children && (
        <div className='right nopadding'>
          <div className='submit'>{children}</div>
        </div>
      )}
    </div>
  </div>
)

Footer.defaultProps = {
  children: null,
}

Footer.propTypes = {
  children: PropTypes.node,
}

export default Footer
