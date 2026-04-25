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

export const updateGameStatus = (id, status) => {
  return request({
    url: `/games/${id}/status`,
    method: 'patch',
    data: { status },
  });
};

export const uploadGameImages = (gameId, formData) => {
  return request({
    url: `/games/${gameId}/images`,
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  });
};

export const getGameImages = (gameId) => {
  return request({
    url: `/games/${gameId}/images`,
    method: 'get',
  });
};
