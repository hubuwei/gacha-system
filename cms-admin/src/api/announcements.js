import request from '../utils/request';

export const getAnnouncements = (params) => {
  return request({
    url: '/announcements',
    method: 'get',
    params,
  });
};

export const getAnnouncementById = (id) => {
  return request({
    url: `/announcements/${id}`,
    method: 'get',
  });
};

export const createAnnouncement = (data) => {
  return request({
    url: '/announcements',
    method: 'post',
    data,
  });
};

export const updateAnnouncement = (id, data) => {
  return request({
    url: `/announcements/${id}`,
    method: 'put',
    data,
  });
};

export const deleteAnnouncement = (id) => {
  return request({
    url: `/announcements/${id}`,
    method: 'delete',
  });
};
