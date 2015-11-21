package cats
package laws
package discipline

import org.scalacheck.Arbitrary
import org.scalacheck.Prop
import Prop._
import org.typelevel.discipline.Laws

trait MonoidalTests[F[_]] extends Laws {
  def laws: MonoidalLaws[F]

  def monoidal[A : Arbitrary, B : Arbitrary, C : Arbitrary](implicit
    iso: MonoidalTests.Isomorphisms[F],
    ArbFA: Arbitrary[F[A]],
    ArbFB: Arbitrary[F[B]],
    ArbFC: Arbitrary[F[C]],
    EqFABC: Eq[F[(A, B, C)]]
  ): RuleSet = {
    new DefaultRuleSet(
      name = "monoidal",
      parent = None,
      "monoidal associativity" -> forAll((fa: F[A], fb: F[B], fc: F[C]) => iso.associativity(laws.associativity(fa, fb, fc)))
    )
  }
}

object MonoidalTests {
  def apply[F[_] : Monoidal](implicit ev: Isomorphisms[F]): MonoidalTests[F] =
    new MonoidalTests[F] { val laws: MonoidalLaws[F] = MonoidalLaws[F] }

  trait Isomorphisms[F[_]] {
    def associativity[A, B, C](fs: (F[(A, (B, C))], F[((A, B), C)]))(implicit EqFABC: Eq[F[(A, B, C)]]): Prop
  }

  object Isomorphisms {
    import algebra.laws._
    implicit def covariant[F[_]](implicit F: Functor[F]): Isomorphisms[F] =
      new Isomorphisms[F] {
        def associativity[A, B, C](fs: (F[(A, (B, C))], F[((A, B), C)]))(implicit EqFABC: Eq[F[(A, B, C)]]) =
          F.map(fs._1) { case (a, (b, c)) => (a, b, c) } ?== F.map(fs._2) { case ((a, b), c) => (a, b, c) }
      }
    implicit def contravariant[F[_]](implicit F: functor.Contravariant[F]): Isomorphisms[F] =
      new Isomorphisms[F] {
        def associativity[A, B, C](fs: (F[(A, (B, C))], F[((A, B), C)]))(implicit EqFABC: Eq[F[(A, B, C)]]) =
          F.contramap[(A, (B, C)), (A, B, C)](fs._1) { case (a, b, c) => (a, (b, c)) } ?== F.contramap[((A, B), C), (A, B, C)](fs._2) { case (a, b, c) => ((a, b), c) }
      }
  }

}