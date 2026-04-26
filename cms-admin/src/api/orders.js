import request from '../utils/request';

export const getOrders = (params) => {
  return request({
    url: '/orders',
    method: 'get',
    params,
  });
};

export const getOrderById = (id) => {
  return request({
    url: `/orders/${id}`,
    method: 'get',
  });
};

export const updateOrderStatus = (id, orderStatus) => {
  return request({
    url: `/orders/${id}/status`,
    method: 'put',
    params: { orderStatus },
  });
};
