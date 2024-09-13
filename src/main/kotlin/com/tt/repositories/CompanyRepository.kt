package com.tt.repositories

import com.tt.models.BusinessId
import com.tt.models.Company
import org.jooq.DSLContext
import org.jooq.generated.tt.tables.Company.COMPANY
import org.jooq.generated.tt.tables.records.CompanyRecord

class CompanyRepository() {
    fun insertCompany(dsl: DSLContext, company: Company) =
        dsl.insertInto(COMPANY)
            .set(toCompanyRecord(company))
            .execute()

    fun upsertCompany(dsl: DSLContext, company: Company) =
        dsl.insertInto(COMPANY)
            .set(toCompanyRecord(company))
            .onConflict(COMPANY.BUSINESS_ID)
            .doUpdate()
            .set(toCompanyRecord(company))
            .execute()

    fun getCompany(dsl: DSLContext, businessId: BusinessId): Company? =
        dsl.selectFrom(COMPANY)
            .where(COMPANY.BUSINESS_ID.eq(businessId))
            .fetch()
            .firstOrNull()
            ?.toCompany()

    fun delete(dsl: DSLContext, businessId: BusinessId) =
        dsl.deleteFrom(COMPANY)
            .where(COMPANY.BUSINESS_ID.eq(businessId))
            .execute()

    private fun CompanyRecord.toCompany(): Company =
        Company(
            businessId = businessId,
            createdAt = createdAt,
            updatedAt = updatedAt,
            name = name,
            status = Company.Status.valueOf(status)
        )

    fun getCompanies(dsl: DSLContext): List<Company> =
        dsl.selectFrom(COMPANY).fetch().map { it.toCompany() }
}

private fun toCompanyRecord(company: Company): CompanyRecord =
    CompanyRecord().apply {
        businessId = company.businessId
        name = company.name
        status = company.status.name
    }
