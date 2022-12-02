import '../../../styles/fetch.scss'

import get from 'lodash.get'
import PropTypes from 'prop-types'
import React, {useContext, useRef, useState,} from 'react'
import {toast} from 'react-toastify'
import Modal from 'react-responsive-modal'
import {CopyToClipboard} from 'react-copy-to-clipboard'
import FileSaver from 'file-saver'
import useWindowsUtils from '../../utils/WindowsUtils'
import {InitializrContext} from '../../reducer/Initializr'
import {getProject, getQueryString} from '../../utils/ApiUtils'
import {AppContext} from '../../reducer/App'

function Fetch({ open, onClose }) {
    const windowsUtils = useWindowsUtils()
    const origin = `${windowsUtils.origin}`
    const { values } = useContext(InitializrContext)
    const { dependencies } = useContext(AppContext)

    const config = get(dependencies, 'list')

    const params = getQueryString(values, config)

    const gitUrl = 'git clone "' + `${origin}/${params}/${values.meta.artifact}.git` + '" ' + `${values.meta.artifact}`
    const downloadUrl = `${origin}/starter.zip?${params}`

    const [button, setButton] = useState('复制')
    const input = useRef(null)
    const onCopy = () => {
        setButton('已复制!')
        setTimeout(() => {
          onClose()
          setButton('复制')
        }, 500)
    }

    const onDownload = async () => {
        const url = `${windowsUtils.origin}/starter.zip`
        const project = await getProject(
          url,
          values,
          get(dependencies, 'list')
        ).catch(() => {
          toast.error(`Could not connect to server. Please check your network.`)
        })
        FileSaver.saveAs(project, `${get(values, 'meta.artifact')}.zip`)
        onClose()
        setButton('复制')
    }
  return (
    <div>
        <Modal
            open={open}
            onClose={() => {
              onClose()
              setButton('复制')
            }}
            classNames={{ modal: 'modal-fetch', overlay: 'overlay' }}
            center
            >
          <div>
              <h2>获取完整项目代码</h2>
              <div>
                <div class="fetch-method control">
                    <span>下载代码包：</span>
                    <input class="control-input" value={downloadUrl} readonly />
                        <a
                            href='/#'
                            onClick={e => {
                                e.preventDefault()
                                onDownload()
                            }}
                        >下载</a>
                </div>
                <div class="fetch-method control">
                    <span>Git Clone 命令：</span>
                    <input class="control-input" value={gitUrl} readonly />
                    <CopyToClipboard onCopy={onCopy} text={gitUrl}>
                    <a
                        href='/#'
                        onClick={e => {
                            e.preventDefault()
                        }}
                    >{button}</a>
                    </CopyToClipboard>
                </div>
              </div>
          </div>
        </Modal>
    </div>
  )
}

Fetch.propTypes = {
  open: PropTypes.bool.isRequired,
  onClose: PropTypes.func.isRequired,
}

export default Fetch
