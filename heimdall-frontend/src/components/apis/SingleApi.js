import React, { Component } from 'react'
import { connect } from 'react-redux'
import { bindActionCreators } from 'redux'
import { getApiById, updateApi, deleteApi, resetApiAction } from '../../actions/apis'
import { getAllEnvironments, clearEnvironments } from '../../actions/environments'

import PageHeader from '../ui/PageHeader' // best way?
import { Card, Row, Tabs, Button, Icon } from 'antd'

import ApiDefinition from './ApiDefinition'
import Resources from '../../containers/Resources'
import Interceptors from '../../containers/Interceptors'

import Loading from '../ui/Loading'

const TabPane = Tabs.TabPane;

class SingleApi extends Component {

    componentDidMount() {
        let idApi = this.props.match.params.id
        if (idApi) {
            this.props.getApiById(idApi)
            this.props.getAllEnvironments()
        }
    }

    componentWillUnmount() {
        this.props.clearEnvironments()
        this.props.resetApiAction()
    }

    // modal methods
    showModal = () => {
        this.setState({
            visible: true,
        })
    }

    handleCancel = (e) => {
        this.setState({
            visible: false,
        })
    }

    render() {
        if (!this.props.api || !this.props.environments) return <Loading />

        const { api } = this.props

        return (
            <div>
                <PageHeader title="APIs" icon="api" />
                <Row>
                    <Card style={{ width: '100%' }} title={api.name}>
                        <Tabs defaultActiveKey="1">
                            <TabPane tab="Definitions" key="1">
                                <ApiDefinition api={api} environments={this.props.environments} history={this.props.history} submit={this.props.updateApi} deleteApi={this.props.deleteApi} />
                            </TabPane>
                            <TabPane tab="Resources" key="2">
                                <Resources api={api} />
                            </TabPane>
                            <TabPane tab="Interceptors" key="3">
                                <Interceptors api={api} />
                            </TabPane>
                        </Tabs>
                    </Card>
                </Row>
                <Row className="h-row">
                    <Button type="primary" onClick={() => this.props.history.push('/apis')} >
                        <Icon type="left" /> Back to APIs
                    </Button>
                </Row>
            </div>
        )
    }
}

const mapStateToProps = (state) => {
    return {
        api: state.apis.api,
        environments: state.environments.environments
    }
}

const mapDispatchToProps = (dispatch) => {
    return {
        getApiById: bindActionCreators(getApiById, dispatch),
        updateApi: bindActionCreators(updateApi, dispatch),
        resetApiAction: bindActionCreators(resetApiAction, dispatch),
        getAllEnvironments: bindActionCreators(getAllEnvironments, dispatch),
        clearEnvironments: bindActionCreators(clearEnvironments, dispatch),
        deleteApi: bindActionCreators(deleteApi, dispatch),
    }
}

export default connect(mapStateToProps, mapDispatchToProps)(SingleApi);