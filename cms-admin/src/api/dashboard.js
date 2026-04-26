import request from '../utils/request';

export const getDashboardStats = () => {
  return request({
    url: '/dashboard/stats',
    method: 'get',
  });
};

export const getRevenueStats = (params) => {
  return request({
    url: '/dashboard/weekly-revenue',
    method: 'get',
    params,
  });
};

export const getTopGames = (limit = 10) => {
  return request({
    url: '/dashboard/popular-games',
    method: 'get',
    params: { limit },
  });
};
