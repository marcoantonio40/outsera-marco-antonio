package com.outsera.awards.adapter.in.web.response;

public record ApiErrorResponse(int status, String error, String message) {
}
