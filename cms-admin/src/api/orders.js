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

export const updateOrderStatus = (id, status) => {
  return request({
    url: `/orders/${id}/status`,
    method: 'patch',
    data: { status },
  });
};

export const getOrdersStats = (params) => {
  return request({
    url: '/orders/stats',
    method: 'get',
    params,
  });
};
