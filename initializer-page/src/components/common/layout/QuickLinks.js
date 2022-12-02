import get from 'lodash.get'
import React, {useContext, useEffect, useRef, useState} from 'react'
import {CSSTransition, TransitionGroup} from 'react-transition-group'

import {AppContext} from '../../reducer/App'
import {IconCaretDown, IconHelp} from '../icons'
import {Switch} from '../form'

const QuickLinks = () => {
    const {theme, dispatch, language} = useContext(AppContext)
    const [help, setHelp] = useState(false)
    const wrapper = useRef(null)
    const toggleTheme = () => {
        const newTheme = theme === 'dark' ? 'light' : 'dark'
        dispatch({
            type: 'UPDATE',
            payload: {
                theme: newTheme,
            },
        })
    }
    const toggleLanguage = () => {
        const newLanguage = language === 'en' ? 'zh' : 'en'
        dispatch({
            type: 'UPDATE',
            payload: {
                language: newLanguage,
            },
        })
    }
    useEffect(() => {
        const clickOutside = event => {
            const children = get(wrapper, 'current')
            if (children && !children.contains(event.target)) {
                setHelp(false)
            }
        }
        document.addEventListener('mousedown', clickOutside)
        return () => {
            document.removeEventListener('mousedown', clickOutside)
        }
    }, [setHelp])

    /*
    <li>
        <span className='switch-language'>
          <Switch id='language-switch' isOn={language === 'en'} onChange={toggleLanguage} />
          {language === 'en' ? 'English' : '中文'}
        </span>
    </li>
    */
    return (
        <ul className='quick-links'>
            <li>
               <span className='switch-mode'>
                  <Switch id='theme-switch' isOn={theme === 'dark'} onChange={toggleTheme}/>
                    {theme === 'dark' ? '深色' : '浅色'} 主题
                </span>
            </li>
            <li>
                <a
                    href='/'
                    className='dropdown'
                    tabIndex='-1'
                    onClick={e => {
                        e.preventDefault()
                        setHelp(!help)
                    }}
                    ref={wrapper}
                >
                    <IconHelp/>
                    帮助信息
                    <IconCaretDown className='caret'/>
                </a>

                <TransitionGroup component={null}>
                    {help && (
                        <CSSTransition classNames='nav-anim' timeout={500}>
                            <ul className='dropdown-menu'>
                                <li>
                                    <a
                                        id='ql-help-projects'
                                        target='_blank'
                                        rel='noopener noreferrer'
                                        href='https://spring.io/projects'
                                        tabIndex='-1'
                                    >
                                        Spring Projects
                                    </a>
                                </li>
                                <li>
                                    <a
                                        id='ql-help-guides'
                                        target='_blank'
                                        rel='noopener noreferrer'
                                        tabIndex='-1'
                                        href='https://spring.io/projects/spring-cloud/'
                                    >
                                        Spring Cloud
                                    </a>
                                </li>
                                <li>
                                    <a
                                        id='ql-help-guides'
                                        target='_blank'
                                        rel='noopener noreferrer'
                                        tabIndex='-1'
                                        href='https://github.com/alibaba/spring-cloud-alibaba/blob/master/README-zh.md'
                                    >
                                        Spring Cloud Alibaba
                                    </a>
                                </li>
                            </ul>
                        </CSSTransition>
                    )}
                </TransitionGroup>
            </li>
        </ul>
    )
}

export default QuickLinks
