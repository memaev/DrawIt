package com.llc.drawit.domain.entities;

import lombok.Builder;

@Builder
public record Whiteboard(
    String id,
    String name,
    String members
) {}
