package com.oronaminc.join.emoji.dao;

import com.oronaminc.join.emoji.domain.Emoji;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmojiRepository extends JpaRepository<Emoji, String> {

}
