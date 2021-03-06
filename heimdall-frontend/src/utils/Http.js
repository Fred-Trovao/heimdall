import axios from 'axios'
// import config from '../config'

const HTTP = axios.create({
     baseURL: process.env.REACT_APP_SCHEME + '://' + process.env.REACT_APP_ADDRESS + ':' + process.env.REACT_APP_PORT,
})

const HTTPv1 = axios.create({
    baseURL: process.env.REACT_APP_SCHEME + '://' + process.env.REACT_APP_ADDRESS + ':' + process.env.REACT_APP_PORT + process.env.REACT_APP_API,
    headers: {'Content-Type': 'application/json'}
})

HTTPv1.interceptors.request.use(req => {
    if (localStorage.getItem('user')) {
        req.auth = JSON.parse(localStorage.getItem('user'))
        // console.log(req.auth)
    }
     return req
}, error => Promise.reject(error))

HTTPv1.interceptors.response.use(res => {
    if (res.status === 401) {
        console.log('access denied')
        localStorage.removeItem('user')
    }
    return res
}, error => Promise.reject(error))

export {HTTP, HTTPv1};