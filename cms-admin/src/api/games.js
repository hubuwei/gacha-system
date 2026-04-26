import request from '../utils/request';

export const getGames = (params) => {
  return request({
    url: '/games',
    method: 'get',
    params,
  });
};

export const getGameById = (id) => {
  return request({
    url: `/games/${id}`,
    method: 'get',
  });
};

export const createGame = (data) => {
  return request({
    url: '/games',
    method: 'post',
    data,
  });
};

export const updateGame = (id, data) => {
  return request({
    url: `/games/${id}`,
    method: 'put',
    data,
  });
};

export const deleteGame = (id) => {
  return request({
    url: `/games/${id}`,
    method: 'delete',
  });
};

export const updateGameStatus = (id, isPublished) => {
  return request({
    url: `/games/${id}/status`,
    method: 'put',
    params: { isPublished },
  });
};
