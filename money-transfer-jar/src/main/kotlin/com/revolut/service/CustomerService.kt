package com.revolut.service

import com.revolut.dao.DaoFactory.customerDao
import com.revolut.response.ErrorCode
import com.revolut.response.Response
import com.revolut.model.Customer
import com.revolut.utils.getNow
import java.util.concurrent.atomic.AtomicLong

/**
 * CustomerService
 *
 * Created by yaroslav
 */


class CustomerService: Service<Customer> {
    private val dao = customerDao

    private val nextId: AtomicLong = AtomicLong(0)

    override fun getEntity(id: Long): Response<Customer> {
        return dao.read(id)
    }

    fun create(customerPersonalData: String): Response<Customer> {
        return dao.create(Customer(
                id = generateId(),
                timestamp = getNow(),
                dataHash = customerPersonalData
        ))
    }

    fun read(id: Long): Response<Customer> {
        return dao.read(id)
    }

    fun update(id: Long, customerPersonalData: String): Response<Customer> {
        return dao.update(Customer(
                id = id,
                timestamp = getNow(),
                dataHash = customerPersonalData
        ))
    }

    fun delete(id: Long): Response<Customer> {
        return dao.delete(id)
    }

    fun block(customerId: Long): Response<Customer> {
        val response = dao.read(customerId)
        if (response.error != ErrorCode.OK) {
            return Response(null, ErrorCode.CUSTOMER_NOT_FOUND)
        }
        response.entity?.apply { isBlocked = true }
                ?: return Response(null, ErrorCode.CUSTOMER_NOT_FOUND)

        return dao.update(response.entity)
    }

    private fun generateId(): Long {
        return nextId.incrementAndGet()
    }
}