package com.playrole.service.image;

public record ProcessedImage(byte[] bytes, String extension, String contentType, int width, int height) {
}
