package com.vqn.bizflow.backend.exception

class ResourceNotFoundException(message: String) : RuntimeException(message)

class DuplicateException(message: String) : RuntimeException(message)

class BadRequestException(message: String) : RuntimeException(message)

class UnauthorizedException(message: String) : RuntimeException(message)

class ForbiddenException(message: String) : RuntimeException(message)

class ConflictException(message: String) : RuntimeException(message)