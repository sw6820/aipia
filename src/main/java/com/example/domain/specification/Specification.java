package com.example.domain.specification;

/**
 * Specification pattern interface for encapsulating business rules.
 * Provides a flexible way to combine and reuse business logic.
 */
public interface Specification<T> {
    
    /**
     * Checks if the given object satisfies this specification.
     */
    boolean isSatisfiedBy(T candidate);
    
    /**
     * Combines this specification with another using AND logic.
     */
    default Specification<T> and(Specification<T> other) {
        return new AndSpecification<>(this, other);
    }
    
    /**
     * Combines this specification with another using OR logic.
     */
    default Specification<T> or(Specification<T> other) {
        return new OrSpecification<>(this, other);
    }
    
    /**
     * Negates this specification.
     */
    default Specification<T> not() {
        return new NotSpecification<>(this);
    }
    
    /**
     * AND specification implementation.
     */
    class AndSpecification<T> implements Specification<T> {
        private final Specification<T> left;
        private final Specification<T> right;
        
        public AndSpecification(Specification<T> left, Specification<T> right) {
            this.left = left;
            this.right = right;
        }
        
        @Override
        public boolean isSatisfiedBy(T candidate) {
            return left.isSatisfiedBy(candidate) && right.isSatisfiedBy(candidate);
        }
    }
    
    /**
     * OR specification implementation.
     */
    class OrSpecification<T> implements Specification<T> {
        private final Specification<T> left;
        private final Specification<T> right;
        
        public OrSpecification(Specification<T> left, Specification<T> right) {
            this.left = left;
            this.right = right;
        }
        
        @Override
        public boolean isSatisfiedBy(T candidate) {
            return left.isSatisfiedBy(candidate) || right.isSatisfiedBy(candidate);
        }
    }
    
    /**
     * NOT specification implementation.
     */
    class NotSpecification<T> implements Specification<T> {
        private final Specification<T> specification;
        
        public NotSpecification(Specification<T> specification) {
            this.specification = specification;
        }
        
        @Override
        public boolean isSatisfiedBy(T candidate) {
            return !specification.isSatisfiedBy(candidate);
        }
    }
}