package com.tt.services

import arrow.core.Either
import arrow.core.raise.Raise
import arrow.core.raise.catch
import arrow.core.raise.either
import arrow.core.raise.ensure
import arrow.core.raise.ensureNotNull
import com.tt.client.TestClient
import com.tt.client.TestReply
import com.tt.models.BusinessId
import com.tt.models.Company
import com.tt.models.DbFailure
import com.tt.models.Failure
import com.tt.models.NotFoundFailure
import com.tt.models.SomeFailure
import com.tt.models.TestClientFailure
import com.tt.repositories.CompanyRepository
import org.jooq.DSLContext

class CompanyService(
    private val companyRepository: CompanyRepository,
    private val testClient: TestClient,
    dslContext: DSLContext
) : Transactional(dslContext) {

    suspend fun getCompany(businessId: BusinessId): Either<Failure, Company> =
        either {
            catch(
                {
                    ensure(fetchDummyData(businessId).value == 1) { SomeFailure("Value is not 1") }
                    ensureNotNull(companyRepository.getCompany(dsl, businessId)) { NotFoundFailure("Company $businessId doesn't exist") }
                },
                { raise(DbFailure(it)) }
            )
        }

    fun getCompaniesByStatus(status: Company.Status): Either<Failure, List<Company>> =
        either {
            catch(
                { companyRepository.getCompanies(dsl).filter { it.status == status } },
                { raise(DbFailure(it)) }
            )
        }

    fun insertCompany(company: Company): Either<Failure, String> =
        either {
            catch(
                { companyRepository.insertCompany(dsl, company).let { "Company stored correctly" } },
                { raise(DbFailure(it)) }
            )
        }

    fun upsertCompany(company: Company): Either<Failure, String> =
        either {
            catch(
                { companyRepository.upsertCompany(dsl, company).let { "Company stored correctly" } },
                { raise(DbFailure(it)) }
            )
        }

    fun deleteCompany(businessId: BusinessId): Either<Failure, String> =
        either {
            catch(
                { companyRepository.delete(dsl, businessId).let { "Company deleted successfully" } },
                { raise(DbFailure(it)) }
            )
        }

    context(Raise<Failure>)
    private suspend fun fetchDummyData(businessId: BusinessId): TestReply =
        catch(
            { testClient.testFetch(businessId) },
            { raise(TestClientFailure(it)) }
        )
}
