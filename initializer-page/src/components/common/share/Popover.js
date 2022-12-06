import PropTypes from 'prop-types'
import get from 'lodash.get'
import React, {useEffect, useRef, useState} from 'react'
import {CSSTransition, TransitionGroup} from 'react-transition-group'
import {CopyToClipboard} from 'react-copy-to-clipboard'

import useWindowsUtils from '../../utils/WindowsUtils'
import {IconTimes} from '../icons'

function Popover({ shareUrl, shareSrc, open, onClose, position }) {
  const [button, setButton] = useState('Copy')
  const wrapper = useRef(null)
  const input = useRef(null)
  const link = useRef(null)
  const windowsUtils = useWindowsUtils()
  useEffect(() => {
    const clickOutside = event => {
      const children = get(wrapper, 'current')
      if (children && !children.contains(event.target)) {
        onClose()
      }
    }
    document.addEventListener('mousedown', clickOutside)
    if (get(input, 'current')) {
      get(input, 'current').focus()
    }
    return () => {
      document.removeEventListener('mousedown', clickOutside)
    }
  }, [onClose, input])

  const onEnter = () => {
    setButton('复制')
  }

  const onCopy = () => {
    setButton('已复制!')
    input.current.focus()
    setTimeout(() => {
      onClose()
    }, 500)
  }

  const urlToShare = `${windowsUtils.origin}/bootstrap.html/${shareSrc}#!${shareUrl}`
  return (
    <>
      <TransitionGroup component={null}>
        {open && (
          <CSSTransition onEnter={onEnter} classNames='popup' timeout={500}>
            <div
              className='popup-share'
              style={{
                top: `${position.y - 200}px`,
                left: `${position.x - 200}px`,
              }}
            >
              <div ref={wrapper}>
                <div className='popup-header'>
                  <h1>分享你的配置</h1>
                  <a
                    href='/#'
                    onClick={e => {
                      e.preventDefault()
                      onClose()
                    }}
                    className='close'
                  >
                    <IconTimes />
                  </a>
                </div>
                <div className='popup-content'>
                  {/* eslint-disable-next-line */}
                  <label htmlFor='input-share'>
                    请复制下面的链接来分享你的项目配置；
                  </label>
                  <div className='control'>
                    <input
                      onFocus={event => {
                        event.target.select()
                      }}
                      id='input-share'
                      className={`control-input ${
                        button === 'Copied!' ? 'padding-lg' : ''
                      }`}
                      readOnly
                      value={urlToShare}
                      ref={input}
                    />
                    <CopyToClipboard onCopy={onCopy} text={urlToShare}>
                      <a
                        href='/#'
                        onClick={e => {
                          e.preventDefault()
                        }}
                        className='link'
                        ref={link}
                      >
                        {button}
                      </a>
                    </CopyToClipboard>
                  </div>
                </div>
              </div>
            </div>
          </CSSTransition>
        )}
      </TransitionGroup>
      {open && (
        <button
          className='button primary share-ghost'
          type='button'
          style={{
            top: `${position.y}px`,
            left: `${position.x}px`,
          }}
        >
          分享...
        </button>
      )}
    </>
  )
}

Popover.propTypes = {
  shareUrl: PropTypes.string.isRequired,
  shareSrc: PropTypes.string.isRequired,
  open: PropTypes.bool.isRequired,
  onClose: PropTypes.func.isRequired,
  position: PropTypes.shape({
    x: PropTypes.number.isRequired,
    y: PropTypes.number.isRequired,
  }).isRequired,
}

export default Popover
