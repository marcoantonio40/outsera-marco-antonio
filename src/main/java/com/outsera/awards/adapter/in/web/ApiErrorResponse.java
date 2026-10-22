package com.outsera.awards.adapter.in.web;

public record ApiErrorResponse(int status, String error, String message) {
}
