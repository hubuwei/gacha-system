import request from '../utils/request';

export const getUsers = (params) => {
  return request({
    url: '/users',
    method: 'get',
    params,
  });
};

export const updateUserStatus = (id, accountStatus) => {
  return request({
    url: `/users/${id}/status`,
    method: 'put',
    params: { accountStatus },
  });
};
