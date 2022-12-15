import BodyClassName from 'react-body-classname'
import get from 'lodash.get'
import React, {lazy, Suspense, useContext, useEffect, useRef, useState,} from 'react'
import {toast} from 'react-toastify'

import useHash from './utils/Hash'
import useWindowsUtils from './utils/WindowsUtils'
import {AppContext} from './reducer/App'
import {Button, Form, RadioGroup} from './common/form'
import {
    Control,
    FieldError,
    FieldInput,
    FieldRadio,
    List,
    Loading,
    PanelMore,
    QuickSearch,
    Tabs,
    Warnings,
} from './common/builder'
import {Footer, Layout} from './common/layout'
import {InitializrContext} from './reducer/Initializr'
import {getConfig, getInfo, getProject, getQueryString} from './utils/ApiUtils'

const Explore = lazy(() => import('./common/explore/Explore.js'))
const Share = lazy(() => import('./common/share/Share.js'))
const Fetch = lazy(() => import('./common/fetch/Fetch.js'))
const HotKeys = lazy(() => import('./common/builder/HotKeys.js'))

export default function Application() {
  const {
    complete,
    config,
    more,
    tab,
    dispatch,
    theme,
    fetch: fetchOpen,
    share: shareOpen,
    explore: exploreOpen,
    dependencies,
  } = useContext(AppContext)
  const { values, share, dispatch: dispatchInitializr, errors } = useContext(InitializrContext)

  const [positionShare, setPositionShare] = useState({ x: 0, y: 0 })

  const [blob, setBlob] = useState(null)


  const inputMore = useRef(null)
  const inputQuickSearch = useRef(null)

  const windowsUtils = useWindowsUtils()
  useHash()

  useEffect(() => {
    if (windowsUtils.origin) {
      const url = `${windowsUtils.origin}/metadata/client`
      getInfo(url).then(jsonConfig => {
        const response = getConfig(jsonConfig)
        dispatchInitializr({ type: 'COMPLETE', payload: { ...response } })
        dispatch({ type: 'COMPLETE', payload: response })
      })
    }
  }, [dispatch, dispatchInitializr, windowsUtils.origin])

  const onSubmit = async () => {
    dispatch({ type: 'FETCH_UPDATE', payload: { open: true } })
  }

  const onExplore = async () => {
    const url = `${windowsUtils.origin}/starter.zip`
    dispatch({ type: 'EXPLORE_UPDATE', payload: { open: true } })
    const project = await getProject(
      url,
      values,
      get(dependencies, 'list')
    ).catch(() => {
      toast.error(`Could not connect to server. Please check your network.`)
    })
    setBlob(project)
  }

  const onShare = event => {
    const { x, y } = get(event, 'target').getBoundingClientRect()
    setPositionShare({ x, y })
    dispatch({ type: 'SHARE_UPDATE', payload: { open: true } })
  }

  const update = args => {
    dispatchInitializr({ type: 'UPDATE', payload: args })
  }

  const onRunInSandbox = () => {
    const origin = `${windowsUtils.origin}`
    const config = get(dependencies, 'list')
    const params = getQueryString(values, config, ["cloudshell"])

    const gitUrl = `${origin}/${params}/${values.meta.artifact}.git`

    const handsOnUrl = `${origin}/handson?${params}`

    window.open(handsOnUrl)

  }

  const onExport = () => {
    const origin = `${windowsUtils.origin}`
    const config = get(dependencies, 'list')
    const params = getQueryString(values, config)

    const codeupUrl = `${origin}/codeup?${params}`

    window.open(codeupUrl)
  }

  let shareSrc = get(values, 'share')
  if(shareSrc){
    shareSrc = `${shareSrc}/`
  } else {
    shareSrc = ''
  }

  return (
    <Layout>
      <BodyClassName className={theme} />
      <Suspense fallback=''>
        <HotKeys onSubmit={onSubmit} onExplore={onExplore} />
      </Suspense>
      <Form onSubmit={onSubmit}>
        {!complete ? (
          <Loading />
        ) : (
          <>
            <Warnings />
            <Control text='项目构建方式'>
              <RadioGroup
                name='project'
                selected={get(values, 'project')}
                options={get(config, 'lists.project')}
                onChange={value => {
                  update({ project: value })
                }}
              />
            </Control>
            <Control text='开发语言'>
              <RadioGroup
                name='language'
                selected={get(values, 'language')}
                options={get(config, 'lists.language')}
                onChange={value => {
                  update({ language: value })
                }}
              />
            </Control>
            <Control text='Spring Boot版本'>
              <RadioGroup
                name='boot'
                selected={get(values, 'boot')}
                error={get(errors, 'boot.value', '')}
                options={get(config, 'lists.boot')}
                onChange={value => {
                  dispatchInitializr({
                    type: 'UPDATE',
                    payload: { boot: value },
                    config: get(dependencies, 'list'),
                  })
                  dispatch({
                    type: 'UPDATE_DEPENDENCIES',
                    payload: { boot: value },
                  })
                }}
              />
              {get(errors, 'boot') && (
                <FieldError>
                  Spring Boot {get(errors, 'boot.value')} is not supported.
                  Please select a valid version
                </FieldError>
              )}
            </Control>
            <Control text='项目基本信息' variant='md'>
              <FieldInput
                id='input-group'
                value={get(values, 'meta.group')}
                text='Group'
                onChange={event => {
                  update({ meta: { group: event.target.value } })
                }}
              />
              <FieldInput
                id='input-artifact'
                value={get(values, 'meta.artifact')}
                text='Artifact'
                onChange={event => {
                  update({ meta: { artifact: event.target.value } })
                }}
              />
              <PanelMore fieldFocusOnOpen={inputMore}>
                <FieldInput
                  id='input-name'
                  value={get(values, 'meta.name')}
                  text='项目名称'
                  disabled={!more}
                  inputRef={inputMore}
                  onChange={event => {
                    update({ meta: { name: event.target.value } })
                  }}
                />
                <FieldInput
                  id='input-description'
                  value={get(values, 'meta.description')}
                  text='项目描述'
                  disabled={!more}
                  onChange={event => {
                    update({ meta: { description: event.target.value } })
                  }}
                />
                <FieldInput
                  id='input-packageName'
                  value={get(values, 'meta.packageName')}
                  text='基础包'
                  disabled={!more}
                  onChange={event => {
                    update({ meta: { packageName: event.target.value } })
                  }}
                />
                <FieldRadio
                  id='input-packaging'
                  value={get(values, 'meta.packaging')}
                  text='打包方式'
                  disabled={!more}
                  options={get(config, 'lists.meta.packaging')}
                  onChange={value => {
                    update({ meta: { packaging: value } })
                  }}
                />
                <FieldRadio
                  id='input-java'
                  value={get(values, 'meta.java')}
                  text='Java版本'
                  disabled={!more}
                  options={get(config, 'lists.meta.java')}
                  onChange={value => {
                    update({ meta: { java: value } })
                  }}
                />
              </PanelMore>
            </Control>
            <Control text='应用架构'>
              <RadioGroup
                name='architecture'
                selected={get(values, 'architecture')}
                options={get(config, 'lists.architecture')}
                onChange={value => {
                  update({ architecture: value })

                  get(values, 'dependencies', []).forEach(item => {
                    dispatchInitializr({
                      type: 'REMOVE_DEPENDENCY',
                      payload: { id:item },
                    })
                  })

                  get(config, 'lists.architecture', [])
                  .filter(item => item.key === value)
                  .forEach(arch => {
                    get(arch, 'dependencies', []).forEach(dep => {
                      dispatchInitializr({
                        type: 'ADD_DEPENDENCY',
                        payload: { id:dep },
                      })
                    })
                  })

                }}
              />
            </Control>
            <Control text='组件与示例' variant={tab === 'quicksearch' ? 'xl' : 'xxl'}>
              <Tabs
                changeTab={newTab => {
                  if (
                    newTab === 'quicksearch' &&
                    get(inputQuickSearch, 'current')
                  ) {
                    get(inputQuickSearch, 'current').focus()
                  }
                }}
              />
              {tab === 'quicksearch' && (
                <QuickSearch submit={onSubmit} input={inputQuickSearch} />
              )}
              {tab === 'list' && <List />}
            </Control>
            <Footer>
              <Button
                id='generate-project'
                variant='primary'
                onClick={onSubmit}
                hotkey={`${windowsUtils.symb} + ⏎`}
              >
                获取代码
              </Button>
              <Button
                id='explore-project'
                onClick={onExplore}
                hotkey='Ctrl + Space'
              >
                浏览代码
              </Button>
              <Button id='share-project' onClick={onShare}>
                分享...
              </Button>
            </Footer>
          </>
        )}
      </Form>
      <Suspense fallback=''>
        <Share
          open={shareOpen || false}
          shareUrl={share}
          shareSrc={shareSrc}
          position={positionShare}
          onClose={() => {
            dispatch({
              type: 'SHARE_UPDATE',
              payload: { open: false },
            })
          }}
        />
        <Explore
          projectName={`${get(values, 'meta.artifact')}.zip`}
          blob={blob}
          open={exploreOpen || false}
          onClose={() => {
            dispatch({
              type: 'EXPLORE_UPDATE',
              payload: { open: false },
            })
            setBlob(null)
          }}
        />
        <Fetch
          open={fetchOpen || false}
          onClose={() => {
            dispatch({
              type: 'FETCH_UPDATE',
              payload: { open: false },
            })
          }}
        />
      </Suspense>
    </Layout>
  )
}
