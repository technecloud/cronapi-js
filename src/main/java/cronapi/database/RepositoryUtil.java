package cronapi.database;

import javax.persistence.EntityManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaContext;
import org.springframework.stereotype.Component;

@Component
public class RepositoryUtil
{
    @Autowired
    private JpaContext jpaContext;

    public EntityManager getEntityManager(Class clz)
    {
        return jpaContext.getEntityManagerByManagedType(clz);
    }
}