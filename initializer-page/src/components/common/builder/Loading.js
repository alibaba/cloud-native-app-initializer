import React from 'react'

import Control from './Control'
import {Footer} from '../layout'
import {Placeholder} from '../form'

export default function Loading() {
  return (
    <>
      <Control text='项目构建方式'>
        <Placeholder type='radios' count={2} width='133px' />
      </Control>
      <Control text='开发语言'>
        <Placeholder type='radios' count={3} width='73px' />
      </Control>
      <Control text='Spring Boot 版本'>
        <Placeholder type='radios' count={5} width='105px' />
      </Control>
      <Control text='项目元信息' special='md'>
        <div>
          <div className='control'>
            <Placeholder type='input' />
          </div>
          <div className='control'>
            <Placeholder type='input' />
          </div>
          <div className='control'>
            <Placeholder type='dropdown' />
          </div>
        </div>
      </Control>
      <Control text='组件与示例'>
        <Placeholder type='tabs' count={2} />
      </Control>
      <Footer>
        <Placeholder type='button' width='189px' />
        <Placeholder type='button' width='212px' />
        <Placeholder type='button' width='110px' />
      </Footer>
    </>
  )
}
