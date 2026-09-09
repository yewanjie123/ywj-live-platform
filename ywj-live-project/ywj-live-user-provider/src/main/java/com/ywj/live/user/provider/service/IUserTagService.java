package com.ywj.live.user.provider.service;

import com.ywj.live.user.constants.UserTagsEnum;

public interface IUserTagService {
    boolean containTag(Long userId, UserTagsEnum userTagsEnum);

    boolean cancel(Long userId, UserTagsEnum userTagsEnum);

    boolean setTage(Long userId, UserTagsEnum userTagsEnum);
}