package com.kassandra.service;

import com.kassandra.modal.Coin;
import com.kassandra.modal.User;
import com.kassandra.modal.Watchlist;

public interface WatchlistService {

    Watchlist findUserWatchlist(Long userId) throws Exception;
    Watchlist createWatchlist(User user);
    Watchlist findById(Long id) throws Exception;

    Coin addItemToWatchlist(Coin coin, User user) throws Exception;

}
