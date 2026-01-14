import axiosClient from "../api/axiosClient";

export const authService = {
login: (username, password) => {
return axiosClient.post('/auth/token', { username, password });
},
register: (userData) => {
// userData format: { username, password, fullname, email, address: {...} }
return axiosClient.post('/users', userData);
},
getMyInfo: () => {
return axiosClient.get('/users/myInfo');
}
};