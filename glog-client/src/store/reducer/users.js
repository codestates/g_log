import {
    createAction,
    handleActions
} from 'redux-actions';
import {
    produce
} from 'immer';
import axios from 'axios';

// // axios.defaults.baseURL = process.env.REACT_APP_SERVER_URL;
// console.log('baseURL ::: ', axios.defaults.baseURL);

// Acion Type
const SIGN_IN = 'SIGN_IN';
const SIGN_OUT = 'SIGN_OUT';
const GET_USER = 'GET_USER';
const SET_USER = 'SET_USER';

// init Action
const SignOut = createAction(SIGN_OUT, (user) => ({
    user
}));
const getUser = createAction(GET_USER, (user) => ({
    user
}));
const setUser = createAction(SET_USER, (user) => ({
    ...user
}));

const initialState = {
    user: null,
    xuserid: '',
    accessToken: '',
    is_login: false
};



const signupAPI = (mail, pwd, niknm) => {
    console.log("signup")
    return function (dispatch, getState, {
        history
    }) {
        axios({
            url: '/signup',
            method: 'POST',
            withCredentials: true,
            data:{
                "mail": mail,
                "pwd": pwd,
                "nikNm": niknm
            }
        }).then((res) => {
                if (res.status === 200) {
                    window.alert('회원가입이 되었습니다!');
                    window.location.href = "/";
                }
        }).catch((res)=>{
            window.alert(
                '중복된 아이디 입니다.',
            );
        })
    };
};

const signinAPI = (id, pw) => {
    console.log(id + " : " + pw);
    return function (dispatch, getState, {
        history
    }) {

        axios({
                url: '/signin',
                method: 'post',
                data: {
                    "mail": id,
                    "pwd": pw
                },
                withCredentials: true
            })
            .then((res) => {
                console.log('로그인 : ', res);
                if (res.status === 200) {
                    axios.defaults.headers.common[
                        'authorization'
                    ] = `Bearer ${res.data.data}`;
                    axios.defaults.headers.common[
                        'X-USER-ID'
                    ] = `${id}`;
                    window.sessionStorage.setItem('key', res.data.data);
                    window.sessionStorage.setItem('mail', id);
                    dispatch(
                        setUser({
                            accessToken: `Bearer ${res.data.data}`
                        })
                    );
                    dispatch(getUserInfo());

                } else {
                    alert('로그인에 실패했습니다');
                }
            })
            .catch((err) => {
                console.error(err);
                alert('로그인에 실패했습니다');
            });
    };
};

const googleOAuthSignInAPI = (code) => {
    console.log(code);
    return function (dispatch, getState, {
        history
    }) {
        axios({
                url: '/oauth/google',
                method: 'post',
                data: {
                    authorizationCode: `${code}`
                },
                withCredentials: true
            })
            .then((res) => {
                console.log('로그인 : ', res);
                if (res.status === 200) {
                    window.sessionStorage.setItem('key', res.data.data.accessToken);
                    window.sessionStorage.setItem('mail', res.data.data.userId);
                    dispatch(
                        setUser({
                            accessToken: `Bearer ${res.data.data}`
                        })
                    );
                    dispatch(getUserInfo());
                } else {
                    alert('로그인에 실패했습니다');
                }
            })
            .catch((err) => {
                console.error(err);
                alert('로그인에 실패했습니다');
            });
    };
};


const getUserInfo = () => {
    console.log("get myinfo");
    console.log(window.sessionStorage.getItem('key'))
    console.log(window.sessionStorage.getItem('mail'))
    return function (dispatch, getState, {
        history
    }) {
        axios({
            url: '/myinfo',
            method: 'get',
            withCredentials: true,
            headers: {
                'authorization': `Bearer ${window.sessionStorage.getItem('key')}`,
                'X-USER-ID': window.sessionStorage.getItem('mail')
            }
        }).then((res) => {
            console.log('getUserInfo', res);
            dispatch(
                setUser({
                    user: res.data.data,
                    xuserid: res.data.data.mail
                })
            );

            history.replace('/');
        });
    };
};


const setUserInfo = (nik,img,tilte) => {
    console.log("set myinfo");
    return function (dispatch, getState, {
        history
    }) {
        axios({
            url: '/myinfo',
            method: 'post',
            withCredentials: true,
            headers: {
                'authorization': `Bearer ${window.sessionStorage.getItem('key')}`,
                'X-USER-ID': window.sessionStorage.getItem('mail')
            },
            data : {
                "mail":window.sessionStorage.getItem('mail'),
                "pwd" :"",
                "nikNm" :nik,
                "imgNm":img,
                "glogTitle":tilte,
            }
        }).then((res) => {
            console.log('getUserInfo', res);
            dispatch(getUserInfo());
        });
    };
};

const loginCheck = () => {
    return function (dispatch, getState, {
        history
    }) {
        const token = localStorage.getItem('accessToken');
        if (token) {
            dispatch(
                setUser({
                    username: 'username',
                    nickname: 'nickname',
                    kakaoId: 'kakaoId'
                })
            );
        } else {
            dispatch(logoutCheck());
        }
    };
};
const logoutCheck = () => {
    return function (dispatch, getState, {
        history
    }) {
        axios({
                url: '/sinout',
                method: 'get',
                withCredentials: true,
                headers: {
                    'authorization': sessionStorage.getItem('key'),
                    'X-USER-ID': sessionStorage.getItem('mail')
                }
            })
            .then((res) => {
                localStorage.removeItem('state');
                window.alert('로그 아웃 되었습니다.');
            })
            .catch((err) => {
                window.alert('로그 아웃 실패하였습니다.');
            });
        dispatch(SignOut());
        history.replace('/');
    };
};

const isLogin = () => {
    const token = localStorage.getItem('token');

    if (!token) {
        return false;
    }
    return true;
};

// reducer: handleActions(immer를 통한 불변성 유지)
export default handleActions({
        [SET_USER]: (state, action) =>

            produce(state, (draft) => {
                console.log("produce " + action);
                draft.user = action.payload.user === undefined ? draft.user : action.payload.user;
                draft.accessToken = (action.payload.accessToken === undefined ? draft.accessToken : action.payload.accessToken)
                draft.xuserid = (action.payload.xuserid === undefined ? draft.xuserid : action.payload.xuserid);
                draft.is_login = true;
            }),
        [SIGN_OUT]: (state, action) =>
            produce(state, (draft) => {
                draft.user = null;
                draft.is_login = false;
                draft.accessToken = null;
                draft.xuserid = null;
            })
    },
    initialState
);

const actionCreators = {
    SignOut,
    googleOAuthSignInAPI,
    getUser,
    signupAPI,
    signinAPI,
    isLogin,
    loginCheck,
    logoutCheck,
    getUserInfo
};

export {
    actionCreators
};